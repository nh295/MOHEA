function selbool= newfilter(CF)

x1 = [0, 1];
x2 = [1, 0];
[a, b] = line2point(x1, x2);

totalsize = 101;

x = 0:0.01:1;
y = a.*x + b;

%scatter(x, y);
%hold on;
% scatter(CF(:,1),CF(:,2));

at = -a.*ones(1,totalsize);
bt = ones(1,totalsize);
for i=1:totalsize
    bt(i) = line1pointwithgr(at(i), [x(i),y(i)]);
    %plotline(at(i), bt(i));
end
xlim([0,1]);
ylim([0,1]);

ss = size(CF,1);
selbool = false(1,ss);
for i=1:totalsize
    mind = 100000;
    sel = -1;
    
    for j=1:ss
        if (~selbool(j))
            point = CF(j,:);
            bt2 = line1pointwithgr(a,point);
            [x2,y2] = line2crosspoint([a, bt2],[at(i),bt(i)]);
            d = (point(1)-x2)^2 + (point(2)-y2)^2;
            if (d<mind)
                mind = d;
                sel = j;
            end
        end
    end
    
    if (sel~=-1)
        selbool(sel) = true;
    end
end

% disp(count);
% scatter(CF(selbool,1),CF(selbool,2));

end

% the equation for line
% y = a*x + b;
% a = [a,b]
% point = [x,y]

function [a,b] = line2point(x1,x2)
x = [x1;x2];
y = x(:,2)';
x = x(:,1);
one = ones(2,1);
x = [x one];
at = y/x;
a = at(1);
b = at(2);
end

% the equation for line
% y = a*x + b;
function b = line1pointwithgr(a,x1)
  b = x1(2)-a*x1(1);
end

function [x,y] = line2crosspoint(a1,a2)
  if (a1(1) ==a2(1))  
      disp('Wrong, parallel lines');
      x = inf;y = inf;
      return;
  end

  x = (a2(2)-a1(2))/(a1(1)-a2(1));
  y = a1(1)*x + a1(2);  
end

function plotline(a, b)
    x = 0:0.001:1;
    y = a.*x +b;
    scatter(x,y,1);
end
